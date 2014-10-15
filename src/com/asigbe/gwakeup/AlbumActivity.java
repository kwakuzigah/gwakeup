package com.asigbe.gwakeup;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView.ScaleType;

/**
 * This activity is launch when the alarm is triggered. It plays the music and
 * allows to stop it or snooze.
 * 
 */
public class AlbumActivity extends Activity implements OnChildClickListener,
		OnClickListener {

	private static final int GROUP_ID_COLUMN_INDEX = 0;

	private static final String[] ALBUMS_PROJECTION = new String[] {
			BaseColumns._ID, AlbumColumns.ALBUM, AlbumColumns.ALBUM_ART,
			AlbumColumns.ARTIST, AlbumColumns.NUMBER_OF_SONGS };

	private static final String[] MUSIC_PROJECTION = new String[] {
			BaseColumns._ID, MediaColumns.TITLE, AudioColumns.ALBUM_ID,
			AudioColumns.TRACK };

	private static final int TOKEN_GROUP = 0;
	private static final int TOKEN_CHILD = 1;

	private QueryHandler mQueryHandler;
	private SimpleCursorTreeAdapter mAdapter;

	private TreeSet<Integer> albums;

	private TreeSet<String> musics;
	private MediaPlayer mediaPlayer;

	private static final class QueryHandler extends AsyncQueryHandler {
		private CursorTreeAdapter mAdapter;

		public QueryHandler(Context context, CursorTreeAdapter adapter) {
			super(context.getContentResolver());
			this.mAdapter = adapter;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			switch (token) {
			case TOKEN_GROUP:
				this.mAdapter.setGroupCursor(cursor);
				break;

			case TOKEN_CHILD:
				int groupPosition = (Integer) cookie;
				this.mAdapter.setChildrenCursor(groupPosition, cursor);
				break;
			}
		}
	}

	public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

		private final TreeSet<String> musics;

		// Note that the constructor does not take a Cursor. This is done to
		// avoid querying the
		// database on the main thread.
		public MyExpandableListAdapter(Context context, TreeSet<String> musics,
				int groupLayout, int childLayout, String[] groupFrom,
				int[] groupTo, String[] childrenFrom, int[] childrenTo) {

			super(context, null, groupLayout, groupFrom, groupTo, childLayout,
					childrenFrom, childrenTo);
			this.musics = musics;
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			// Given the group, we return a cursor for all the children within
			// that group

			// Return a cursor that points to this album's songs
			// Uri.Builder builder =
			// MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.buildUpon();
			// ContentUris.appendId(builder, groupCursor
			// .getLong(GROUP_ID_COLUMN_INDEX));
			// builder.appendEncodedPath(MediaStore.Audio.Data.CONTENT_DIRECTORY);
			// Uri phoneNumbersUri = builder.build();

			AlbumActivity.this.mQueryHandler.startQuery(
					AlbumActivity.TOKEN_CHILD, groupCursor.getPosition(),
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					AlbumActivity.MUSIC_PROJECTION, AudioColumns.ALBUM_ID
							+ "=?", new String[] { groupCursor
							.getString(AlbumActivity.GROUP_ID_COLUMN_INDEX) },
					null);

			return null;
		}

		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			// TODO Auto-generated method stub
			super.bindGroupView(view, context, cursor, isExpanded);
			int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
			String title = cursor.getString(cursor
					.getColumnIndex(AlbumColumns.ALBUM));
			String artist = cursor.getString(cursor
					.getColumnIndex(AlbumColumns.ARTIST));
			String art = cursor.getString(cursor
					.getColumnIndex(AlbumColumns.ALBUM_ART));

			TextView textView = (TextView) view.findViewById(R.id.album_name);
			textView.setTextSize(15);

			ImageView imageView = (ImageView) view.findViewById(R.id.album_art);
			if (art != null) {
				imageView.setImageURI(Uri.fromFile(new File(art)));
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
			} else {
				imageView.setImageDrawable(null);
			}
			if (AlbumActivity.this.albums.contains(id)) {
				textView.setText(Html.fromHtml("<font color='#000000'>" + title
						+ "</font><br><font color='#1e1e1e'><i>" + artist
						+ "</i></font>"));
				view.setBackgroundDrawable(AlbumActivity.this.albumGradient);
			} else {
				textView.setText(Html.fromHtml("<font color='#ffffff'>" + title
						+ "</font><br><font color='#aeaeae'><i>" + artist
						+ "</i></font>"));
				view.setBackgroundDrawable(null);
			}
		}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {
			// TODO Auto-generated method stub
			super.bindChildView(view, context, cursor, isLastChild);
			int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
			String uri = ContentUris.withAppendedId(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString();
			String name = cursor.getString(cursor
					.getColumnIndex(MediaColumns.TITLE));

			TextView textView = (TextView) view.findViewById(R.id.song_title);
			textView.setTextSize(13);
			if (this.musics.contains(uri)) {
				textView.setText(Html.fromHtml("<font color='#000000'>" + name
						+ "</font>"));
				view
						.setBackgroundDrawable(AlbumActivity.this.selectionGradient);
			} else {
				textView.setText(Html.fromHtml("<font color='#FFFFFF'>" + name
						+ "</font>"));
				view.setBackgroundColor(0xFF222222);
			}
		}
	}

	private ExpandableListView listView;

	private Button saveButton;

	private Button cancelButton;

	private GradientDrawable albumGradient;
	private GradientDrawable selectionGradient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.album);

		Intent intent = getIntent();

		this.albumGradient = new GradientDrawable(Orientation.TOP_BOTTOM,
				new int[] { 0xff398aff, 0xff0059d6 });
		this.selectionGradient = new GradientDrawable(Orientation.TOP_BOTTOM,
				new int[] { 0xff80b5ff, 0xff6b98d6 });

		// load alarms
		this.musics = (TreeSet<String>) intent.getExtras().get(
				AlarmConfigurationActivity.EXTRA_MUSICS);
		this.listView = (ExpandableListView) findViewById(R.id.listView);
		this.saveButton = (Button) findViewById(R.id.okButton);
		this.saveButton.setOnClickListener(this);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);
		this.cancelButton.setOnClickListener(this);

		this.mediaPlayer = new MediaPlayer();
		// Query for people
		// mQueryHandler.startQuery(TOKEN_GROUP, null,
		// MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
		// ALBUMS_PROJECTION, MediaStore.Audio.Albums.NUMBER_OF_SONGS
		// + ">0", null, MediaStore.Audio.Albums.ALBUM + " ASC");
		// mQueryHandler.startQuery(TOKEN_GROUP, null,
		// MediaStore.Audio.Albums.INTERNAL_CONTENT_URI,
		// ALBUMS_PROJECTION, null, null, null);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// gets all albums
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				AlbumActivity.MUSIC_PROJECTION, null, null,
				AudioColumns.TRACK + " DESC");
		this.albums = new TreeSet<Integer>();
		int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
		int albumColumnIndex = cursor.getColumnIndex(AudioColumns.ALBUM_ID);
		if (cursor.moveToFirst()) {
			do {
				String uri = ContentUris.withAppendedId(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						cursor.getInt(idColumnIndex)).toString();
				if (this.musics.contains(uri)) {
					this.albums.add(cursor.getInt(albumColumnIndex));
				}
			} while (cursor.moveToNext());
		}
		cursor.close();

		// Set up our adapter

		this.mAdapter = new MyExpandableListAdapter(this,
				this.musics,
				R.layout.album_line,
				R.layout.song_line,
				new String[] { AlbumColumns.ALBUM }, // Name for
				// group
				// layouts
				new int[] { android.R.id.text1 },
				new String[] { MediaColumns.TITLE }, // Number
				// for
				// child
				// layouts
				new int[] { android.R.id.text1 });

		this.listView.setAdapter(this.mAdapter);
		this.listView.setOnChildClickListener(this);

		this.mQueryHandler = new QueryHandler(this, this.mAdapter);
		this.mQueryHandler.startQuery(AlbumActivity.TOKEN_GROUP, null,
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
				AlbumActivity.ALBUMS_PROJECTION, AlbumColumns.NUMBER_OF_SONGS
						+ ">0", null, AlbumColumns.ALBUM + " ASC");
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.mediaPlayer.release();
		this.mediaPlayer = null;

		Intent intent = getIntent();
		intent.putExtra(AlarmConfigurationActivity.EXTRA_MUSICS, this.musics);
		setResult(Activity.RESULT_OK, intent);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		String uri = ContentUris.withAppendedId(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString();
		boolean albumsIsSelected = false;
		int albumId = (int) this.mAdapter.getGroupId(groupPosition);
		if (this.musics.contains(uri)) {
			this.musics.remove(uri);

			int childrenCount = this.mAdapter.getChildrenCount(groupPosition);
			for (int i = 0; (i < childrenCount) && !albumsIsSelected; i++) {
				long childId = this.mAdapter.getChildId(groupPosition, i);
				String currentUri = ContentUris.withAppendedId(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, childId)
						.toString();
				albumsIsSelected = (this.musics.contains(currentUri));
			}
		} else {
			albumsIsSelected = true;
			this.musics.add(uri);
		}

		if (albumsIsSelected) {
			this.albums.add(albumId);
		} else {
			this.albums.remove(albumId);
		}

		this.mAdapter.notifyDataSetChanged(false);

		if (this.mediaPlayer == null) {
			this.mediaPlayer = new MediaPlayer();
		} else {
			this.mediaPlayer.stop();
			this.mediaPlayer.reset();
		}

		try {
			this.mediaPlayer.setDataSource(uri);
			this.mediaPlayer.prepare();
			this.mediaPlayer.start();
		} catch (IllegalArgumentException e1) {
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}

		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Null out the group cursor. This will cause the group cursor and all
		// of the child cursors
		// to be closed.
		this.mAdapter.changeCursor(null);
		this.mAdapter = null;
	}

	@Override
	public void onClick(View v) {
		if (v == this.saveButton) {
			Intent intent = this.getIntent();
			intent.putExtra(AlarmConfigurationActivity.EXTRA_MUSICS,
					this.musics);
			this.setResult(Activity.RESULT_OK, intent);
		}
		finish();
	}

}
