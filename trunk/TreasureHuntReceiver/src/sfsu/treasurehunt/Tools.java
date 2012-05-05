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
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/*
 * Tools menu. A list of tools you can purchase is shown on the screen.  When user selects
 * a tool on a particular line, a tool information image pops up with a "buy" button.  User
 * can tap on anywhere on the image to make it and the "buy" button disappear or hit the
 * button to purchase the item.  Addition actions may occur depending on the tool purchased
 * before sending the request to the server. 
 */
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

		// Add tools to the tools list.
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

		// Handles the different tool help screens.
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

				purchaseItem = position; // Global variable set for what tool is selected.
				buy.setVisibility(View.VISIBLE);
				buy.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						confirmToolPurchase();
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

	/*
	 * Confirm box for each individual tool to ensure user wishes to purchase tool.
	 */
	public void confirmToolPurchase() {
		switch (purchaseItem) {
		case Taunt:
			callConfirmBox("Taunt");
			break;
		case Monkey:
			callConfirmBox("Monkey");
			break;
		case SmokeScreen:
			callConfirmBox("SmokeScreen");
			break;
		case ClearSky:
			callConfirmBox("ClearSky");
			break;
		case Compass:
			callConfirmBox("Compass");
			break;
		case Stealer:
			callConfirmBox("Stealer");
			break;
		case Lockout:
			callConfirmBox("Lockout");
			break;
		}

		helpScreen.setVisibility(View.GONE);
		buy.setVisibility(View.GONE);
	}

	/*
	 * Alert box pops up asking the user to confirm or cancel the purchase.
	 */
	public void callConfirmBox(String item) {
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
								makeToolPurchase();
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
	
	/*
	 * After the user has confirmed to purchase a particular tool, this method handles
	 * all actions related to executing that request. 
	 */
	private void makeToolPurchase() {
		switch (purchaseItem) {
		case Taunt:
			// Actions for purchasing Taunt.
			break;
		case Monkey:
			// Actions for purchasing Monkey.
			break;
		case SmokeScreen:
			// Actions for purchasing SmokeScreen.
			break;
		case ClearSky:
			// Actions for purchasing ClearSky.
			break;
		case Compass:
			// Actions for purchasing Compass.
			break;
		case Stealer:
			// Actions for purchasing Stealer.
			break;
		case Lockout:
			// Actions for purchasing Lockout.
			break;
		}
	}
}