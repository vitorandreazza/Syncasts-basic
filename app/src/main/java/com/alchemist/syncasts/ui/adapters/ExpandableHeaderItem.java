package com.alchemist.syncasts.ui.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.BasicModel;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.ExpandableViewHolder;

public class ExpandableHeaderItem extends AbstractExpandableHeaderItem
        <ExpandableHeaderItem.ExpandableHeaderViewHolder, EpisodeItem> implements IHolder<BasicModel> {

    private BasicModel mModel;

    public ExpandableHeaderItem() {
    }

    public ExpandableHeaderItem(BasicModel model) {
        mModel = model;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return mModel.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_header;
    }

    @Override
    public ExpandableHeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ExpandableHeaderViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ExpandableHeaderViewHolder holder, int position, List payloads) {
        holder.title.setText(mModel.getTitle());
        holder.subtitle.setText(mModel.getSubtitle());
    }

    @Override
    public BasicModel getModel() {
        return mModel;
    }

    public void setModel(BasicModel model) {
        mModel = model;
    }

    @Override
    public void addSubItem(EpisodeItem subItem) {
        super.addSubItem(subItem);
        Collections.sort(mSubItems);
    }

    public class ExpandableHeaderViewHolder extends ExpandableViewHolder {

        @BindView(R.id.header_title) TextView title;
        @BindView(R.id.header_subtitle) TextView subtitle;
        @BindView(R.id.header_expanded) ImageView expanded;

        public ExpandableHeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);
            ButterKnife.bind(this, view);
        }

        @Override
        protected void toggleExpansion() {
            super.toggleExpansion();
            int position = getFlexibleAdapterPosition();
            if (mAdapter.isExpanded(position)) {
                expanded.setImageResource(R.drawable.ic_collapse_24dp);
            } else {
                expanded.setImageResource(R.drawable.ic_expand_more_24dp);
            }
        }
    }
}
