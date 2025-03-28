package com.example.taskdao;

import java.util.List;

public interface TagDao {
    void addTag(Tag tag);
    void deleteTag(int id);
    List<Tag> getAllTags();
}

