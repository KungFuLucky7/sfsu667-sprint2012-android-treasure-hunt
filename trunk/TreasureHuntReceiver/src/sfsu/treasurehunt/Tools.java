package sfsu.treasurehunt;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Tools extends Activity {

	private Button returnToMap;
	private ListView listView1;
	ArrayList<ToolList> toolListData = new ArrayList<ToolList>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_main);

		toolListData.add(new ToolList(R.drawable.laugh, "Taunt"));
		toolListData.add(new ToolList(R.drawable.monkey, "Dizzy Monkey"));
		toolListData.add(new ToolList(R.drawable.smoke, "Smoke Screen"));
		toolListData.add(new ToolList(R.drawable.sky, "Clear Sky"));
		toolListData.add(new ToolList(R.drawable.compass, "Compass"));
		toolListData.add(new ToolList(R.drawable.thief, "Stealer"));
		toolListData.add(new ToolList(R.drawable.lockout, "Lockout"));
		
		ToolListAdapter adapter = new ToolListAdapter(this, R.layout.listview_item_row, toolListData);
		
		listView1 = (ListView) findViewById(R.id.list);

		View header = (View) getLayoutInflater().inflate(R.layout.listview_header_row, null);
		listView1.addHeaderView(header);

		listView1.setAdapter(adapter);
		
		listView1.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Toast.makeText(getApplicationContext(), toolListData.get(position-1).title, Toast.LENGTH_LONG).show();
			}
		});
		
		returnToMap = (Button) findViewById(R.id.mapButton);
		returnToMap.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}