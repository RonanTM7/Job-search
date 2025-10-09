package com.example.job.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.job.R;
import com.example.job.model.Job;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Job job);
    }

    public JobAdapter(List<Job> jobList, OnItemClickListener onItemClickListener) {
        this.jobList = jobList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.bind(job, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateData(List<Job> newJobs) {
        jobList.clear();
        jobList.addAll(newJobs);
        notifyDataSetChanged();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        private TextView jobTitle, companyName, jobSalary, jobLocation, remoteBadge;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.job_title);
            companyName = itemView.findViewById(R.id.company_name);
            jobSalary = itemView.findViewById(R.id.job_salary);
            jobLocation = itemView.findViewById(R.id.job_location);
            remoteBadge = itemView.findViewById(R.id.remote_badge);
        }

        public void bind(final Job job, final OnItemClickListener listener) {
            jobTitle.setText(job.getTitle());
            companyName.setText(job.getCompany());
            jobSalary.setText(job.getSalary());
            jobLocation.setText(job.getLocation());

            if (job.isRemote()) {
                remoteBadge.setVisibility(View.VISIBLE);
            } else {
                remoteBadge.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(job);
                }
            });
        }
    }
}