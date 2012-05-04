package sfsu.treasurehunt;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class Tools extends Activity {

	private Button returnToMap;
	private Button buy;
	private ListView listView1;
	private ImageView helpScreen;
	private ArrayList<ToolList> toolListData = new ArrayList<ToolList>();

	// Static tools guide
	private static final int Taunt = 0;
	private static final int Monkey = 1;
	private static final int SmokeScreen = 2;
	private static final int ClearSky = 3;
	private static final int Compass = 4;
	private static final int Stealer = 5;
	private static final int Lockout = 6;

	private int purchaseItem;
	final Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_main);
		setTitle(R.string.tools);

		buy = (Button) findViewById(R.id.buyButton);

		toolListData.add(new ToolList(R.drawable.laugh, "Taunt", 10));
		toolListData.add(new ToolList(R.drawable.monkey, "Dizzy Monkey", 100));
		toolListData.add(new ToolList(R.drawable.smoke, "Smoke Screen", 500));
		toolListData.add(new ToolList(R.drawable.clearsky, "Clear Sky", 700));
		toolListData.add(new ToolList(R.drawable.compass, "Compass", 700));
		toolListData.add(new ToolList(R.drawable.thief, "Stealer", 900));
		toolListData.add(new ToolList(R.drawable.lockout, "Lockout", 950));

		ToolListAdapter adapter = new ToolListAdapter(this,
				R.layout.listview_item_row, toolListData);

		listView1 = (ListView) findViewById(R.id.list);

		// View header = (View)
		// getLayoutInflater().inflate(R.layout.listview_header_row, null);
		// listView1.addHeaderView(header);

		listView1.setAdapter(adapter);

		listView1.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case Taunt:
					helpScreen = (ImageView) findViewById(R.id.tauntHelpScreen);
					break;
				case Monkey:
					helpScreen = (ImageView) findViewById(R.id.monkeyHelpScreen);
					break;
				case SmokeScreen:
					helpScreen = (ImageView) findViewById(R.id.smokescreenHelpScreen);
					break;
				case ClearSky:
					helpScreen = (ImageView) findViewById(R.id.clearskyHelpScreen);
					break;
				case Compass:
					helpScreen = (ImageView) findViewById(R.id.compassHelpScreen);
					break;
				case Stealer:
					helpScreen = (ImageView) findViewById(R.id.stealerHelpScreen);
					break;
				case Lockout:
					helpScreen = (ImageView) findViewById(R.id.lockoutHelpScreen);
					break;
				}

				helpScreen.setVisibility(View.VISIBLE);
				helpScreen.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						helpScreen.setVisibility(View.GONE);
						buy.setVisibility(View.GONE);
					}
				});

				purchaseItem = position;
				buy.setVisibility(View.VISIBLE);
				buy.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						makePurchase(purchaseItem);
					}
				});
				// Toast.makeText(getApplicationContext(), toolListData.get(position-1).title, Toast.LENGTH_LONG).show();
			}

		});

		returnToMap = (Button) findViewById(R.id.mapButton);
		returnToMap.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void makePurchase(int purchaseItem) {
		switch (purchaseItem) {
		case Taunt:
			callPurchaseBox("Taunt");
			break;
		case Monkey:
			callPurchaseBox("Monkey");
			break;
		case SmokeScreen:
			callPurchaseBox("SmokeScreen");
			break;
		case ClearSky:
			callPurchaseBox("ClearSky");
			break;
		case Compass:
			callPurchaseBox("Compass");
			break;
		case Stealer:
			callPurchaseBox("Stealer");
			break;
		case Lockout:
			callPurchaseBox("Lockout");
			break;
		}

		helpScreen.setVisibility(View.GONE);
		buy.setVisibility(View.GONE);
	}

	public void callPurchaseBox(String item) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set title
		alertDialogBuilder.setTitle("Do you wish to buy: " + item + "?");

		// set dialog message
		alertDialogBuilder
				// .setMessage("Click yes to exit!")
				.setCancelable(false)
				.setPositiveButton("Confirm",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close current activity
								// AlertBoxActivity.this.finish();
								dialog.cancel();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
}