// Copyright 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

// netdataCaptureCharts:
// Capture metrics from Netdata, generate and store charts.
def call(Map parameters = [:]) {
    def podName = parameters.get('podName', 'default')
    Environment environment = parameters.get('environment')
    timeout(15) {
        try {
            sh(script: "${WORKSPACE}/automation/misc-tools/netdata/capture/capture-charts admin --outdir ${WORKSPACE}/netdata/admin -l ${WORKSPACE}/logs/netdata-capture-admin.log")
        } catch (Exception exc) {
            echo "Failed to capture Netdata charts"
        }
    }
}
