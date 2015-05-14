package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

import java.util.List;

public class MoreChildrenResponse {
    @Expose private MoreChildrenResponseJson json;

    private static class MoreChildrenResponseJson {
        @Expose private List<String> errors;
        @Expose private MoreChildrenResponseData data;

        private static class MoreChildrenResponseData {

            @Expose private List<AbsRedditComment> things;
        }
    }

    public List<AbsRedditComment> getChildComments() {
        return json.data.things;
    }
}
