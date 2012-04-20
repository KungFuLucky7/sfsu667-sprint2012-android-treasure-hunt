package sfsu.treasurehunt;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ToolListAdapter extends ArrayAdapter<ToolList>{

    Context context; 
    int layoutResourceId;    
    ToolList data[] = null;
    ArrayList<ToolList> data2;
    
    public ToolListAdapter(Context context, int layoutResourceId, ToolList[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    
    public ToolListAdapter(Context context, int layoutResourceId, ArrayList<ToolList> data2) {
        super(context, layoutResourceId, data2);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data2 = data2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ToolListHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new ToolListHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            
            row.setTag(holder);
        }
        else
        {
            holder = (ToolListHolder)row.getTag();
        }
        
        //NoteList noteLineItem = data[position];
        ToolList noteLineItem = data2.get(position);
        holder.txtTitle.setText(noteLineItem.title);
        holder.imgIcon.setImageResource(noteLineItem.icon);
        
        return row;
    }
    
   static class ToolListHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }
}
