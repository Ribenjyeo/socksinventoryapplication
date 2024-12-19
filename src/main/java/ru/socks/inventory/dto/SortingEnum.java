package ru.socks.inventory.dto;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum SortingEnum {
    COLOR("color"),
    COTTON_CONTENT("cottonContent");

    private final String type;

    SortingEnum(String type) {
        this.type = type;
    }

    public static SortingEnum fromString(String type) {
        for (SortingEnum sortingEnum : SortingEnum.values()) {
            if (sortingEnum.getType().equalsIgnoreCase(type)) {
                return sortingEnum;
            }
        }
        throw new IllegalArgumentException("Invalid sorting type: " + type);
    }

    public static Sort getSort(String sortBy) {
        Sort sort = Sort.unsorted();
        if (sortBy != null) {
            SortingEnum sortingEnum = SortingEnum.fromString(sortBy);
            sort = Sort.by(Sort.Direction.ASC, sortingEnum.getType());
        }

        return sort;
    }

}
