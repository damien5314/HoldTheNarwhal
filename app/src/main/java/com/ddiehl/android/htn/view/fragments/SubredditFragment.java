package com.ddiehl.android.htn.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.presenter.SubredditPresenter;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.android.htn.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.android.htn.view.dialogs.ChooseTimespanDialog;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import static android.app.Activity.RESULT_OK;

@FragmentWithArgs
public class SubredditFragment extends BaseListingsFragment implements LinkView {

  public static final String TAG = SubredditFragment.class.getSimpleName();

  @Arg(required = false) String mSubreddit;
  @Arg(required = false) String mSort;
  @Arg(required = false) String mTimespan;

  public SubredditFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FragmentArgs.inject(this);
    if (TextUtils.isEmpty(mSort)) mSort = "hot";
    if (TextUtils.isEmpty(mTimespan)) mTimespan = "all";
    mLinkPresenter = new SubredditPresenter(
        mMainView, this, this, mSubreddit, mSort, mTimespan);
    mListingsPresenter = (ListingsPresenter) mLinkPresenter;
    mCallbacks = (Callbacks) mListingsPresenter;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.listings_fragment;
  }

  @Override
  public ListingsAdapter getListingsAdapter() {
    return new ListingsAdapter(
        mListingsPresenter, mLinkPresenter, mCommentPresenter, mMessagePresenter);
  }

  //region Options menu
  /** Same implementation in {@link UserProfileFragment} */

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    // Disable timespan option if current sort does not support it
    switch (mSort) {
      case "controversial":
      case "top":
        menu.findItem(R.id.action_change_timespan)
            .setVisible(true);
        break;
      case "hot":
      case "new":
      case "rising":
      default:
        menu.findItem(R.id.action_change_timespan)
            .setVisible(false);
        break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_change_sort:
        showSortOptionsMenu();
        mAnalytics.logOptionChangeSort();
        return true;
      case R.id.action_change_timespan:
        showTimespanOptionsMenu();
        mAnalytics.logOptionChangeTimespan();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showSortOptionsMenu() {
    ChooseLinkSortDialog chooseLinkSortDialog =
        ChooseLinkSortDialog.newInstance(mSort);
    chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
    chooseLinkSortDialog.show(getFragmentManager(), ChooseLinkSortDialog.TAG);
  }

  private void showTimespanOptionsMenu() {
    ChooseTimespanDialog chooseTimespanDialog =
        ChooseTimespanDialog.newInstance(mTimespan);
    chooseTimespanDialog.setTargetFragment(this, REQUEST_CHOOSE_TIMESPAN);
    chooseTimespanDialog.show(getFragmentManager(), ChooseTimespanDialog.TAG);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_CHOOSE_SORT:
        if (resultCode == RESULT_OK) {
          String sort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
          onSortSelected(sort);
        }
        break;
      case REQUEST_CHOOSE_TIMESPAN:
        if (resultCode == RESULT_OK) {
          String timespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
          onTimespanSelected(timespan);
        }
        break;
    }
  }

  // Cache for sort selected before showing timespan dialog
  private String mSelectedSort;

  private void onSortSelected(@NonNull String sort) {
    mAnalytics.logOptionChangeSort(sort);

    if (sort.equals(mSort)) return;

    if (sort.equals("top") || sort.equals("controversial")) {
      mSelectedSort = sort;
      showTimespanOptionsMenu();
    } else {
      mSort = sort;
      getActivity().invalidateOptionsMenu();
      mListingsPresenter.onSortChanged(mSort, mTimespan);
    }
  }

  private void onTimespanSelected(@NonNull String timespan) {
    mAnalytics.logOptionChangeTimespan(timespan);

    mSort = mSelectedSort;
    mTimespan = timespan;
    getActivity().invalidateOptionsMenu();
    mListingsPresenter.onSortChanged(mSort, mTimespan);
  }

  //endregion
}
