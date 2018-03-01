// Copyright 2017 SUSE LINUX GmbH, Nuernberg, Germany.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
import com.suse.kubic.Environment


Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    // Run Transactional Update
    stage('TX Update') {
        environment.minions.each { minion ->
            def runTransactionalUpdate = {
                // This run daily - avoid the risk of a race condition during the tests
                // Since this timer runs nightly, we want to disable and stop it before we run
                // transactional-update ourselves.
                shOnMinion(minion: minion, script: 'systemctl disable --now transactional-update.timer')
                shOnMinion(minion: minion, script: '/usr/sbin/transactional-update cleanup dup salt')
            }

            parallelSteps.put("${minion.role}-${minion.index}", runTransactionalUpdate)
        }

        timeout(90) {
            parallel(parallelSteps)
        }
    }
}
