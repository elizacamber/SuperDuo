package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eliza on 8/12/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            Cursor cursor;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }
                // This method is called by the launcher. However, our ContentProvider is not exported so it doesn't
                // have access to the data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String currentDate= dateFormat.format(new Date(System.currentTimeMillis()));
                String[] args = new String[] { currentDate };

                cursor= getContentResolver().query(
                        DatabaseContract.scores_table.buildScoreWithDate(),
                        null,
                        null,
                        args,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }

                final RemoteViews views = new RemoteViews(getPackageName(), R.layout.scores_list_item);
                views.setTextViewText(R.id.home_name, cursor.getString(scoresAdapter.COL_HOME));
                views.setTextColor(R.id.home_name, getResources().getColor(R.color.dark_text));
                views.setTextViewTextSize(R.id.home_name, TypedValue.COMPLEX_UNIT_DIP,10);

                views.setTextViewText(R.id.away_name, cursor.getString(scoresAdapter.COL_AWAY));
                views.setTextColor(R.id.away_name, getResources().getColor(R.color.dark_text));
                views.setTextViewTextSize(R.id.away_name, TypedValue.COMPLEX_UNIT_DIP,10);

                views.setTextViewText(R.id.score_textview, Utilies.getScores(
                        cursor.getInt(scoresAdapter.COL_HOME_GOALS),cursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
                views.setTextColor(R.id.score_textview, getResources().getColor(R.color.dark_text));
                views.setTextViewTextSize(R.id.score_textview, TypedValue.COMPLEX_UNIT_DIP,10);

                views.setImageViewResource(R.id.home_crest,
                        Utilies.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_HOME)));
                views.setImageViewResource(R.id.away_crest,
                        Utilies.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_AWAY)));
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor.moveToPosition(position))
                    return cursor.getLong(cursor.getColumnIndex(DatabaseContract.scores_table._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
