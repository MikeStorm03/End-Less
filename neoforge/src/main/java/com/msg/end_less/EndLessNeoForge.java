package com.msg.end_less;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(EndLessConstants.ID)
public class EndLessNeoForge {

    public EndLessNeoForge(IEventBus eventBus) {

        EndLessCommon.init();
    }
}