/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.fragments.dialogs;

import static org.odk.collect.android.utilities.ViewUtils.pxFromDp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.RankingListAdapter;
import org.odk.collect.android.fragments.viewmodels.RankingViewModel;
import org.odk.collect.android.utilities.RankingItemTouchHelperCallback;
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils;

import java.util.ArrayList;
import java.util.List;

public class RankingWidgetDialog extends DialogFragment {
    private RankingListener listener;
    private RankingListAdapter rankingListAdapter;
    private List<SelectChoice> items;
    private FormEntryPrompt formEntryPrompt;
    private RankingViewModel viewModel;

    public interface RankingListener {
        void onRankingChanged(List<SelectChoice> items);
    }

    public RankingWidgetDialog() {
    }

    public RankingWidgetDialog(List<SelectChoice> items, FormEntryPrompt formEntryPrompt) {
        this.items = new ArrayList<>(items);
        this.formEntryPrompt = formEntryPrompt;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof RankingListener) {
            listener = (RankingListener) context;
        }
        viewModel = new ViewModelProvider(this, new RankingViewModel.Factory(items, formEntryPrompt)).get(RankingViewModel.class);
        if (viewModel.getItems() == null) {
            dismiss();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(getActivity())
                .setView(setUpRankingLayout())
                .setPositiveButton(org.odk.collect.strings.R.string.ok, (dialog, id) -> {
                    listener.onRankingChanged(rankingListAdapter.getItems());
                    dismiss();
                })
                .setNegativeButton(org.odk.collect.strings.R.string.cancel, (dialog, id) -> dismiss())
                .create();
    }

    private NestedScrollView setUpRankingLayout() {
        LinearLayout rankingLayout = new LinearLayout(getContext());
        rankingLayout.setOrientation(LinearLayout.HORIZONTAL);
        rankingLayout.addView(setUpPositionsLayout());
        rankingLayout.addView(setUpRecyclerView());

        float standardMargin = requireContext()
                .getResources()
                .getDimension(org.odk.collect.androidshared.R.dimen.margin_standard);
        int standardMarginPx = pxFromDp(requireContext(), standardMargin);
        rankingLayout.setPadding(standardMarginPx, standardMarginPx, standardMarginPx, standardMarginPx);

        NestedScrollView scrollView = new NestedScrollView(getContext());
        scrollView.addView(rankingLayout);
        return scrollView;
    }

    private LinearLayout setUpPositionsLayout() {
        LinearLayout positionsLayout = new LinearLayout(getContext());
        positionsLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 10, 0);
        positionsLayout.setLayoutParams(layoutParams);

        for (SelectChoice item : viewModel.getItems()) {
            FrameLayout positionLayout = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.ranking_item, positionsLayout, false);
            TextView textView = positionLayout.findViewById(R.id.rank_item_text);
            textView.setText(String.valueOf(viewModel.getItems().indexOf(item) + 1));
            textView.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());

            positionsLayout.addView(positionLayout);
        }
        return positionsLayout;
    }

    private RecyclerView setUpRecyclerView() {
        rankingListAdapter = new RankingListAdapter(viewModel.getItems(), viewModel.getFormEntryPrompt());

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(rankingListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Callback callback = new RankingItemTouchHelperCallback(rankingListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return recyclerView;
    }
}
