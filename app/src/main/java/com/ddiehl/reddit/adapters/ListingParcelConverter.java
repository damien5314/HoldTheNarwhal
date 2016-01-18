package com.ddiehl.reddit.adapters;

import android.os.Parcel;

import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.PrivateMessage;
import com.ddiehl.reddit.listings.Subreddit;
import com.ddiehl.reddit.listings.Trophy;

import org.parceler.ParcelConverter;
import org.parceler.Parcels;

import java.lang.reflect.Type;

public class ListingParcelConverter implements ParcelConverter<Listing> {
  @Override
  public void toParcel(Listing input, Parcel parcel) {
    parcel.writeString(input.getKind());
    parcel.writeParcelable(Parcels.wrap(input.getClass(), input), 0);
  }

  @Override
  public Listing fromParcel(Parcel parcel) {
    String kind = parcel.readString();
    Type type = null;
    switch (kind) {
      case "t1":
        type = Comment.Data.class; break;
      case "t3":
        type = Link.Data.class; break;
      case "t4":
        type = PrivateMessage.Data.class; break;
      case "t5":
        type = Subreddit.Data.class; break;
      case "t6":
        type = Trophy.Data.class; break;
      case "more":
        type = CommentStub.Data.class; break;
    }
    if (type == null) return null;
    else return Parcels.unwrap(
        parcel.readParcelable(
            type.getClass().getClassLoader()));
  }

}
