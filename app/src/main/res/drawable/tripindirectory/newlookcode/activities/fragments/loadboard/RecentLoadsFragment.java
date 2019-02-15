package directory.tripin.com.tripindirectory.newlookcode.activities.fragments.loadboard;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentLoadsFragment extends LoadsListBaseFragment {

    public RecentLoadsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("posts")
                .limitToLast(30);
        // [END recent_posts_query]
        return recentPostsQuery;
    }
}
