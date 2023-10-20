package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.EspenCollect;
import org.odk.collect.android.external.InstanceProvider;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.odk.collect.forms.instances.Instance.STATUS_SUBMISSION_FAILED;
import static org.odk.collect.forms.instances.Instance.STATUS_SUBMITTED;

public class InstanceUploaderAdapter extends CursorAdapter {
    private final Consumer<Long> onItemCheckboxClickListener;
    private Set<Long> selected = new HashSet<>();

    public InstanceUploaderAdapter(Context context, Cursor cursor, Consumer<Long> onItemCheckboxClickListener) {
        super(context, cursor);
        this.onItemCheckboxClickListener = onItemCheckboxClickListener;
        EspenCollect.getInstance().getComponent().inject(this);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.form_chooser_list_item_multiple_choice, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        long lastStatusChangeDate = getCursor().getLong(getCursor().getColumnIndex(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE));
        String status = cursor.getString(cursor.getColumnIndex(DatabaseInstanceColumns.STATUS));

        viewHolder.formTitle.setText(cursor.getString(cursor.getColumnIndex(DatabaseInstanceColumns.DISPLAY_NAME)));
        viewHolder.formSubtitle.setText(InstanceProvider.getDisplaySubtext(context, status, new Date(lastStatusChangeDate)));

        switch (status) {
            case STATUS_SUBMISSION_FAILED:
                viewHolder.statusIcon.setImageResource(R.drawable.ic_form_state_submission_failed);
                break;

            case STATUS_SUBMITTED:
                viewHolder.statusIcon.setImageResource(R.drawable.ic_form_state_submitted);
                break;

            default:
                viewHolder.statusIcon.setImageResource(R.drawable.ic_form_state_finalized);
        }

        long dbId = cursor.getLong(cursor.getColumnIndex(DatabaseInstanceColumns._ID));
        viewHolder.checkbox.setChecked(selected.contains(dbId));
        viewHolder.selectView.setOnClickListener(v -> onItemCheckboxClickListener.accept(dbId));
    }

    public void setSelected(Set<Long> ids) {
        this.selected = ids;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView formTitle;
        TextView formSubtitle;
        CheckBox checkbox;
        ImageView statusIcon;
        ImageView closeButton;
        FrameLayout selectView;

        ViewHolder(View view) {
            formTitle = view.findViewById(R.id.form_title);
            formSubtitle = view.findViewById(R.id.form_subtitle);
            checkbox = view.findViewById(R.id.checkbox);
            statusIcon = view.findViewById(R.id.image);
            closeButton = view.findViewById(R.id.close_box);
            selectView = view.findViewById(R.id.selectView);
        }
    }
}
