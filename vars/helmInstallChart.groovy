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

def call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')
    String releaseName = parameters.get('releaseName')
    String chartName = parameters.get('chartName')
    boolean wait = parameters.get('wait', false)
    Map values = parameters.get('values', [:])

    echo "Deploying Helm chart: ${chartName}"

    String waitFlag = ""
    if (wait) {
        waitFlag = "--wait"
    }

    String safeChartName = chartName.replaceAll('/', '-')

    writeYaml(file: "helm-values-${safeChartName}-${releaseName}.yaml", data: values)
    archiveArtifacts(artifacts: "helm-values-${safeChartName}-${releaseName}.yaml", fingerprint: true)

    withEnv(["KUBECONFIG=${WORKSPACE}/kubeconfig"]) {
        sh(script: "set -o pipefail; ${WORKSPACE}/helm --home ${WORKSPACE}/.helm install ${chartName} ${waitFlag} --namespace ${releaseName} --name ${releaseName} --values helm-values-${safeChartName}-${releaseName}.yaml 2>&1 | tee ${WORKSPACE}/logs/helm-install-${releaseName}-${safeChartName}.log")
    }

    if (wait) {
        sh(script: "${WORKSPACE}/automation/misc-tools/verify-pods-in-ns.sh ${WORKSPACE}/kubeconfig ${releaseName}")
    }

    return releaseName
}
