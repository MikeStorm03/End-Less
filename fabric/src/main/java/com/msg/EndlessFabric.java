/*
 * End-Less Mod
 * Copyright (c) 2025 MikeStorm03
 *
 * Licensed under the GNU General Public License v3.0
 * See the LICENSE file for details.
 */

package com.msg;

import com.msg.dev_folder.TestFunction;
import com.msg.platform.Services;

import net.fabricmc.api.ModInitializer;


public class EndlessFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        EndLessCommon.init();

        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            TestFunction.init();
		}
    }
}
