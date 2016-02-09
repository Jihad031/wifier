package com.iktwo.wifier.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iktwo.wifier.R;
import com.iktwo.wifier.data.AccessPoint;
import com.iktwo.wifier.data.WifiNetwork;
import com.iktwo.wifier.utils.RecyclerItemClickListener;
import com.iktwo.wifier.views.CustomLinearLayout;

import java.util.List;

public class AccessPointsAdapter extends RecyclerView.Adapter<AccessPointsAdapter.ViewHolder> {
    private static final int SINGLE = 0;
    private static final int GROUP = 1;

    private List<AccessPoint> items;
    private int mItemLayout;
    private int mGroupLayout;
    private int mExpandedIndex = -1;

    private RecyclerItemClickListener.OnItemClickListener mOnItemClickListener;

    public AccessPointsAdapter(List<AccessPoint> items, int itemLayout, int groupLayout, RecyclerItemClickListener.OnItemClickListener listener) {
        this.items = items;
        mItemLayout = itemLayout;
        mGroupLayout = groupLayout;
        mOnItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).length() > 1 ? GROUP : SINGLE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;

        switch (viewType) {
            case SINGLE:
                v = LayoutInflater.from(parent.getContext()).inflate(mItemLayout, parent, false);
                break;
            case GROUP:
                v = LayoutInflater.from(parent.getContext()).inflate(mGroupLayout, parent, false);
                break;
        }

        return new ViewHolder(v, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final AccessPoint ap = items.get(position);
        WifiNetwork firstNetwork = ap.getNetworkAt(0);

        holder.ssid.setText(firstNetwork.getSsid());

        if (ap.length() > 1) {
            if (position == mExpandedIndex) {
                holder.innerRecyclerView.setVisibility(View.VISIBLE);
            } else {
                holder.innerRecyclerView.setVisibility(View.GONE);
            }

            /// TODO: here initialize adapter and stuff
            holder.innerRecyclerView.setHasFixedSize(true);
            holder.innerRecyclerView.setLayoutManager(new CustomLinearLayout(holder.itemView.getContext()));
            holder.innerRecyclerView.setItemAnimator(new DefaultItemAnimator());
            holder.innerRecyclerView.setAdapter(new WifiNetworksAdapter(ap.getWifiNetworks(), R.layout.delegate_wifi_network));
        } else {
            holder.bssid.setText(firstNetwork.getBssid());
            holder.manufacturer.setText(firstNetwork.getManufacturer());
        }

        holder.itemView.setTag(ap);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clear() {
        int size = items.size();
        if (size > 0) {
            for (int i = 0; i < size; i++)
                items.remove(0);

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addAccessPoint(AccessPoint ap) {
        items.add(ap);
        notifyItemInserted(items.size());
    }

    public void addWifiNetwork(WifiNetwork network) {
        for (int i = 0; i < items.size(); ++i) {
            WifiNetwork firstNetwork = items.get(i).getNetworkAt(0);

            /// TODO: also check security (CAPABILITIES)!?!
            if (firstNetwork.getSsid().equals(network.getSsid())) {
                items.get(i).addNetwork(network);
                notifyItemChanged(i);
                return;
            }
        }

        items.add(new AccessPoint(network));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerItemClickListener.OnItemClickListener mListener;
        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (innerRecyclerView != null) {
                    if (mExpandedIndex != getAdapterPosition()) {
                        mExpandedIndex = getAdapterPosition();
                    } else {
                        mExpandedIndex = -1;
                    }

                    notifyItemChanged(getAdapterPosition());
                } else {
                    mListener.onItemClick(view, getAdapterPosition());
                }
            }
        };

        public CardView cardView;
        public TextView ssid;
        public TextView bssid;
        public TextView manufacturer;
        public RecyclerView innerRecyclerView;

        public ViewHolder(View view, RecyclerItemClickListener.OnItemClickListener listener) {
            super(view);

            mListener = listener;

            ssid = (TextView) view.findViewById(R.id.text_view_ssid);
            bssid = (TextView) view.findViewById(R.id.text_view_bssid);
            manufacturer = (TextView) view.findViewById(R.id.text_view_manufacturer);
            innerRecyclerView = (RecyclerView) view.findViewById(R.id.inner_recycler_view);
            cardView = (CardView) view.findViewById(R.id.card_view);

            cardView.setOnClickListener(onClickListener);
        }
    }
}

