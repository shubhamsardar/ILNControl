package com.tripin.directory.ilncontrol.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.tripin.directory.ilncontrol.R;

public class ReportedCompViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public TextView time;
    public  TextView reportcount;
    public  TextView status;
    public  TextView call;



    public ReportedCompViewHolder(View itemView) {
        super(itemView);

        status = itemView.findViewById(R.id.status);
        name = itemView.findViewById(R.id.compname);
        time = itemView.findViewById(R.id.time);
        reportcount = itemView.findViewById(R.id.reportcount);
        call = itemView.findViewById(R.id.call);

    }
}