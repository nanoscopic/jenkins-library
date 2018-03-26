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

def call() {
    lock("helm-install-client") {
        // This whole thing is a hack, we should be using our builds of the
        // helm client.
        sh(script: "wget -O /tmp/helm.tar.gz https://kubernetes-helm.storage.googleapis.com/helm-v2.6.1-linux-amd64.tar.gz")
        sh(script: "tar --directory /tmp -xzvf /tmp/helm.tar.gz")
        sh(script: "mv /tmp/linux-amd64/helm ${WORKSPACE}/helm")
        sh(script: "${WORKSPACE}/helm --home ${WORKSPACE}/.helm init --client-only")
        sh(script: "${WORKSPACE}/helm --home ${WORKSPACE}/.helm repo update")
    }
}
