package com.example.valtrak.Data.GameData.DataTransfer.GameData;

public record DeployRequest(
        Long cardId,
        Long strikeGroupId  // null = create a new Strike Group
) {}
