package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.tools.ToolsBDD;



public class HistoryActivity extends SherlockActivity implements OnItemLongClickListener, OnItemClickListener {

	static final int MENU_RESET = 0;
	static final int MENU_SHARE = 2;
	private static int currentId;
	ArrayList<HashMap<String, String>> listItem;
	private ListView lv;
	Button visu;
	Button effacer;
	Dialog dialog;
	String share;
	MyAdapter mSchedule;
	ActionMode mActionMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.listviewcustom);

		lv = (ListView)findViewById(R.id.listviewperso);

		listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();


		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0)
		{
			share = getString(R.string.sharetry);
		}
		else 
		{
			share = getString(R.string.app_name)+" - https://play.google.com/store/apps/details?id=fr.mathis.morpion - ";
			int win = 0;
			int lost = 0;
			int equal = 0;
			c.moveToFirst();
			for(int i = 0; i < c.getCount();i++)
			{
				int n = c.getInt(1);
				if(n == MainActivity.BLUE_PLAYER)
				{
					map = new HashMap<String, String>();
					map.put("titre", "N�"+c.getInt(0)+" - "+getString(R.string.win));
					map.put("description", getString(R.string.winb));
					map.put("img", String.valueOf(R.drawable.croix));
					win++;
				}
				else if(n == MainActivity.RED_PLAYER){
					map = new HashMap<String, String>();
					map.put("titre", "N�"+c.getInt(0)+" - "+getString(R.string.loose));
					map.put("description", getString(R.string.winr));
					map.put("img", String.valueOf(R.drawable.cercle));
					lost++;
				}
				else {
					map = new HashMap<String, String>();
					map.put("titre", "N�"+c.getInt(0)+" - "+getString(R.string.equal));
					map.put("description", getString(R.string.equaltry));
					map.put("img", String.valueOf(R.drawable.icon));
					equal++;
				}
				listItem.add(map);
				c.moveToNext();
			}
			share += (win+lost+equal)+" "+getString(R.string.share1);
			share += " "+win+" "+getString(R.string.share2);
			share += " "+lost+ " " +getString(R.string.share3);
		}

		mSchedule = new MyAdapter(this.getBaseContext(), listItem, R.layout.itemlistviewcustom, new String[] {"img", "titre", "description"}, new int[] {R.id.img, R.id.titre, R.id.description});
		lv.setAdapter(mSchedule);
		lv.setOnItemLongClickListener(this);
		lv.setOnItemClickListener(this);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if(item.getTitle().toString().compareTo(getString(R.string.share))==0)
		{
			share();
		}
		else if(item.getTitle().toString().compareTo(getString(R.string.reset))==0)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
		}
		else {
			int itemId = item.getItemId();
			switch (itemId) {
			case android.R.id.home:
				finish();
				break;
			}
		}

		return true;
	}

	private void share() 
	{
		final Intent MessIntent = new Intent(Intent.ACTION_SEND);
		MessIntent.setType("text/plain");
		MessIntent.putExtra(Intent.EXTRA_TEXT, share);
		HistoryActivity.this.startActivity(Intent.createChooser(MessIntent, getString(R.string.sharewith)));
	}

	private void resetHistory() 
	{
		ToolsBDD.getInstance(this).resetTable();
		Toast.makeText(this, R.string.resethistory, Toast.LENGTH_LONG).show();
		finish();
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		if(pos.size()==0)
		{
			@SuppressWarnings("unchecked")
			HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
			String s = map.get("titre");
			currentId = Integer.parseInt(s.split("N�")[1].split(" ")[0]);

			Bundle objetbunble = new Bundle();
			objetbunble.putString("id", ""+HistoryActivity.currentId);
			Intent intent = new Intent(HistoryActivity.this, VisuActivity.class);
			intent.putExtras(objetbunble);
			startActivity(intent);
		}
		else {
			if (!pos.contains(arg2)) {
				pos.add(arg2);
			}
			else {
				pos = removeInt(pos,arg2);
			}

			if(pos.size()==0)
			{
				mActionMode.finish();
			}
			else {
				if(mActionMode != null)
				{
					if(pos.size()==1)
						mActionMode.setTitle(pos.size()+" "+getString(R.string.s2));
					else mActionMode.setTitle(pos.size()+" "+getString(R.string.s1));
				}
			}
			mSchedule.notifyDataSetChanged();
		}
	}

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case DialogInterface.BUTTON_POSITIVE:
				resetHistory();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};


	/*MENU*/

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SHARE, 0, R.string.share).setIcon(R.drawable.social_share2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(0, MENU_RESET, 0, R.string.reset).setIcon(R.drawable.content_discard2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		/*@SuppressWarnings("unchecked")
		HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
		String s = map.get("titre");
		currentId = Integer.parseInt(s.split("N�")[1].split(" ")[0]);
		final Context c = getApplicationContext();
	    final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.deletegame);
	    dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	        	ToolsBDD.getInstance(c).removePartie(HistoryActivity.currentId);
				dialog.dismiss();
				finish();
				if(listItem.size() > 1)
				{
					Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
					startActivity(intent);
				}
				else {
					resetHistory();
					Toast.makeText(c, getString(R.string.resethistory), Toast.LENGTH_LONG).show();
				}
	        }
	    });
	    dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	dialog.dismiss();
	        }
	    });
	    AlertDialog alert = dialog.create();		
		alert.show();
		 */
		if (!pos.contains(arg2)) {
			if(pos.size()==0)
			{
				mActionMode = startActionMode(mActionModeCallback);
			}
			pos.add(arg2);

		}
		else {
			pos = removeInt(pos,arg2);
		}
		if(pos.size()==0)
		{
			mActionMode.finish();
		}
		if(mActionMode != null)
		{
			if(pos.size()==1)
				mActionMode.setTitle(pos.size()+" "+getString(R.string.s2));
			else mActionMode.setTitle(pos.size()+" "+getString(R.string.s1));
		}
		mSchedule.notifyDataSetChanged();
		return true;
	}

	private ArrayList<Integer> removeInt(ArrayList<Integer> pos2, int arg2) {
		for(int i = 0 ; i < pos2.size() ; i++)
		{
			if(pos2.get(i)==arg2)
			{
				pos2.remove(i);
				break;
			}
		}
		return pos2;
	}


	/*BLOCK THE ROTATION OF THE SCREEN*/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);	    
	}

	ArrayList<Integer> pos = new ArrayList<Integer>();
	private class MyAdapter extends SimpleAdapter {

		public MyAdapter(Context context, List<? extends Map<String, ?>> data,
				int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView,   parent);
			if(pos!=null){
				if (pos.contains(position)) {
					v.setBackgroundColor(Color.LTGRAY);
				}
				else {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
			return v;
		}

	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(0, 50, 0, R.string.reset).setIcon(R.drawable.content_discard2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			return true;
		}

		// Called each time the action mode is shown. Always called after onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case 50:
				for(int num : pos)
				{
					HashMap<String, String> map = (HashMap<String, String>) listItem.get(num);
					String s = map.get("titre");
					currentId = Integer.parseInt(s.split("N�")[1].split(" ")[0]);
					ToolsBDD.getInstance(getApplicationContext()).removePartie(HistoryActivity.currentId);
				}

				Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
				startActivity(intent);
				finish();

				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			pos = new ArrayList<Integer>();
			mSchedule.notifyDataSetChanged();
		}
	};
}