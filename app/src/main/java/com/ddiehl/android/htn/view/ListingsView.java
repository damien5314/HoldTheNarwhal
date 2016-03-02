package com.ddiehl.android.htn.view;

public interface ListingsView
//    extends LinkView, CommentView, PrivateMessageView
{
  void notifyDataSetChanged();
  void notifyItemChanged(int position);
  void notifyItemInserted(int position);
  void notifyItemRemoved(int position);
  void notifyItemRangeChanged(int position, int number);
  void notifyItemRangeInserted(int position, int number);
  void notifyItemRangeRemoved(int position, int number);
  void showSortOptionsMenu();
  void showTimespanOptionsMenu();
  void onSortChanged();
  void onTimespanChanged();
  void scrollToBottom();

  interface Callbacks {
    void onFirstItemShown();
    void onLastItemShown();
  }
}
