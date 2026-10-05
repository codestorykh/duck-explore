package com.duck.explore.jspecify;

import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@NullMarked
@Service
public class TagService {

    // List is non-null, but elements can be null
    public int calculateTotalTagLength(List<@Nullable String> tags) {
        int sum = 0;
        for (String tag : tags) {
            sum += tag.length();

            /*if (tag != null) {
                sum += tag.length(); // Safe: NullAway smart-casts after null guard
            }*/
        }
        return sum;
    }
}