package com.example.uottawa.cali;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AssignmentListAdapter extends ArrayAdapter {

    private Context context;
    private Assignment[] assignments;

    public AssignmentListAdapter(Context context, Assignment[] assignments){
        super(context, R.layout.assignment_list_item, assignments);
        this.context = context;
        this.assignments = assignments;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Assignment assignment = assignments[position];
        if (!assignment.getVisible()) {
            return new View(context);
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.assignment_list_item, parent, false);

        TextView title = (TextView) rowView.findViewById(R.id.assignmentTitleTextViewListElement);

        title.setText(assignment.getCourse().getName() + ": " + assignment.getName());

        TextView dueDate = (TextView) rowView.findViewById(R.id.assignmentDateTextViewListElement);
        dueDate.setText(assignment.getDateString());

        View progress = (View) rowView.findViewById(R.id.assignmentProgressViewListElement);
        View progressInverse = (View) rowView.findViewById(R.id.assignmentProgressInverseViewListElement);
        progress.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, assignment.getComplete()));
        progress.setBackgroundResource(assignment.getCourse().getColorIndex());
        progressInverse.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 100 - assignment.getComplete()));
        progressInverse.setBackgroundResource(assignment.getCourse().getColorInverseIndex());
        return rowView;

    }
}
