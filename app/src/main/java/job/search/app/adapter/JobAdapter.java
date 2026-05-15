package job.search.app.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import job.search.app.R;
import job.search.app.model.Job;
import job.search.app.utils.FormatUtils;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private final List<Job> jobList = new ArrayList<>();
    private final OnItemClickListener onItemClickListener;
    private final OnFavoriteClickListener onFavoriteClickListener;
    private Set<String> favoriteJobIds;

    public interface OnItemClickListener {
        void onItemClick(Job job);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Job job);
    }

    @SuppressLint("NotifyDataSetChanged")
    public JobAdapter(List<Job> initialJobs, Set<String> favoriteJobIds, OnItemClickListener onItemClickListener, OnFavoriteClickListener onFavoriteClickListener) {
        this.jobList.addAll(initialJobs);
        this.favoriteJobIds = favoriteJobIds;
        this.onItemClickListener = onItemClickListener;
        this.onFavoriteClickListener = onFavoriteClickListener;
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
        boolean isFavorite = favoriteJobIds.contains(job.getId());
        holder.bind(job, isFavorite, onItemClickListener, onFavoriteClickListener);
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Job> newJobs) {
        jobList.clear();
        jobList.addAll(newJobs);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateFavorites(Set<String> newFavoriteJobIds) {
        this.favoriteJobIds = newFavoriteJobIds;
        notifyDataSetChanged();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        private final TextView jobTitle;
        private final TextView companyName;
        private final TextView jobSalary;
        private final TextView jobLocation;
        private final TextView remoteBadge;
        private final TextView employmentBadge;
        private final ImageButton favoriteButton;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.job_title);
            companyName = itemView.findViewById(R.id.company_name);
            jobSalary = itemView.findViewById(R.id.job_salary);
            jobLocation = itemView.findViewById(R.id.job_location);
            remoteBadge = itemView.findViewById(R.id.remote_badge);
            employmentBadge = itemView.findViewById(R.id.employment_badge);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
        }

        public void bind(final Job job, final boolean isFavorite, final OnItemClickListener listener, final OnFavoriteClickListener favoriteListener) {
            jobTitle.setText(job.getTitle());
            companyName.setText(job.getCompany());
            jobSalary.setText(FormatUtils.formatSalary(job.getSalary()));
            jobLocation.setText(job.getLocation());

            if (job.isRemote()) {
                remoteBadge.setVisibility(View.VISIBLE);
            } else {
                remoteBadge.setVisibility(View.GONE);
            }

            if (job.getCategory() != null && !job.getCategory().isEmpty()) {
                employmentBadge.setText(job.getCategory());
                employmentBadge.setVisibility(View.VISIBLE);
            } else {
                employmentBadge.setVisibility(View.GONE);
            }

            if (favoriteListener == null) {
                favoriteButton.setVisibility(View.GONE);
            } else {
                favoriteButton.setVisibility(View.VISIBLE);
                favoriteButton.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
                favoriteButton.setOnClickListener(v -> favoriteListener.onFavoriteClick(job));
            }
            itemView.setOnClickListener(v -> listener.onItemClick(job));
        }
    }
}