package com.example.taskdao;

import java.util.ArrayList;
import java.util.List;

public class TagListDao implements TagDao {
    private List<Tag> tags;

    public TagListDao() {
        tags = new ArrayList<>();
    }

    @Override
    public void addTag(Tag tag) {
        tags.add(tag);
    }

    @Override
    public void deleteTag(int id) {
        tags.removeIf(tag -> tag.getId() == id);
    }

    @Override
    public List<Tag> getAllTags() {
        return tags;
    }
}