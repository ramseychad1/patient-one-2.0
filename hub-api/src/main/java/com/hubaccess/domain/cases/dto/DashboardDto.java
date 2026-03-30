package com.hubaccess.domain.cases.dto;

import com.hubaccess.domain.activity.dto.CaseTaskDto;

import java.util.List;

public record DashboardDto(
        List<CaseListItemDto> myCases,
        List<CaseListItemDto> slaBreaches,
        List<CaseTaskDto> openTasks,
        DashboardStats stats
) {
    public record DashboardStats(
            long totalOpen,
            long slaBreached,
            long pendingConsent,
            long pendingPA,
            long pendingFA
    ) {}
}
