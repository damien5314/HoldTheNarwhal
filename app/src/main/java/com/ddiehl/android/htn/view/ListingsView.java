package com.ddiehl.android.htn.view;

public interface ListingsView {

  void notifyDataSetChanged();

  void notifyItemChanged(int position);

  void notifyItemInserted(int position);

  void notifyItemRemoved(int position);

  void notifyItemRangeChanged(int position, int number);

  void notifyItemRangeInserted(int position, int number);

  void notifyItemRangeRemoved(int position, int number);

  interface Callbacks {

    void onFirstItemShown();

    void onLastItemShown();
  }
}
