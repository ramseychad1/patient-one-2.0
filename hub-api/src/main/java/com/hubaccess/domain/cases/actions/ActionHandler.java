package com.hubaccess.domain.cases.actions;

import com.hubaccess.domain.cases.HubCase;
import com.hubaccess.domain.cases.dto.ActionResultDto;
import com.hubaccess.security.AuthenticatedUser;

import java.util.Map;

public interface ActionHandler {
    ActionResultDto.StubResult execute(HubCase hubCase, Map<String, Object> payload, AuthenticatedUser user);
    String getNextState(HubCase hubCase);
    String getNextStage(HubCase hubCase);
    String getNextActionKey(HubCase hubCase);
    String getNextActionLabel(HubCase hubCase);
}
