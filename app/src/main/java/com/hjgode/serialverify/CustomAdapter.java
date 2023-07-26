package com.hjgode.serialverify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<DataModel> {

    private ArrayList<DataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtSerial;
        TextView txtAuftrag;
        TextView txtModel;
        TextView txtBezeichnung;
        TextView txtBemerkung;
        TextView txtID;
    }

    public CustomAdapter(ArrayList<DataModel> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtID = (TextView) convertView.findViewById(R.id.txtID);
            viewHolder.txtSerial = (TextView) convertView.findViewById(R.id.txtSerial);
            viewHolder.txtModel=(TextView)convertView.findViewById(R.id.txtModel);
            viewHolder.txtBezeichnung=(TextView)convertView.findViewById(R.id.txtBezeichnung);
            viewHolder.txtAuftrag = (TextView) convertView.findViewById(R.id.txtAuftrag);
            viewHolder.txtBemerkung=(TextView)convertView.findViewById(R.id.txtBemerkung);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txtID.setText(dataModel.getID());
        viewHolder.txtSerial.setText(dataModel.getSerial());
        viewHolder.txtModel.setText(dataModel.getModel());
        viewHolder.txtBezeichnung.setText(dataModel.getBezeichnung());
        viewHolder.txtAuftrag.setText(dataModel.getAuftrag());
        viewHolder.txtBemerkung.setText(dataModel.getBemerkung());
        // Return the completed view to render on screen
        return convertView;
    }
}