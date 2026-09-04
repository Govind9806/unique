package com.example.uniqueAproovaResidency.module.search.service;

import com.example.uniqueAproovaResidency.module.expense.dto.ExpenseDto;
import com.example.uniqueAproovaResidency.module.expense.service.ExpenseService;
import com.example.uniqueAproovaResidency.module.flat.dto.FlatDto;
import com.example.uniqueAproovaResidency.module.flat.service.FlatService;
import com.example.uniqueAproovaResidency.module.notice.dto.NoticeDto;
import com.example.uniqueAproovaResidency.module.notice.service.NoticeService;
import com.example.uniqueAproovaResidency.module.user.dto.UserDto;
import com.example.uniqueAproovaResidency.module.user.service.UserService;
import com.example.uniqueAproovaResidency.module.work.dto.WorkDto;
import com.example.uniqueAproovaResidency.module.work.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final FlatService flatService;
    private final UserService userService;
    private final WorkService workService;
    private final ExpenseService expenseService;
    private final NoticeService noticeService;

    @Transactional(readOnly = true)
    public Map<String, Object> globalSearch(String query) {
        String q = query != null ? query.toLowerCase() : "";
        Map<String, Object> results = new HashMap<>();

        List<FlatDto> matchingFlats = flatService.getAllFlats().stream()
                .filter(f -> f.getFlatNumber().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<UserDto> matchingUsers = userService.getAllUsers().stream()
                .filter(u -> u.getName().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<WorkDto> matchingWorks = workService.getAllWorks().stream()
                .filter(w -> w.getTitle().toLowerCase().contains(q) || w.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<ExpenseDto> matchingExpenses = expenseService.getAllExpenses().stream()
                .filter(e -> e.getDescription().toLowerCase().contains(q) || e.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());

        List<NoticeDto> matchingNotices = noticeService.getAllNotices().stream()
                .filter(n -> n.getTitle().toLowerCase().contains(q) || n.getDescription().toLowerCase().contains(q))
                .collect(Collectors.toList());

        results.put("flats", matchingFlats);
        results.put("users", matchingUsers);
        results.put("works", matchingWorks);
        results.put("expenses", matchingExpenses);
        results.put("notices", matchingNotices);

        return results;
    }
}
