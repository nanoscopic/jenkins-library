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
// Clones all Kubic Repos.
def call(Map parameters = [:]) {
    def branch = parameters.get('branch')
    boolean ignorePullRequest = parameters.get('ignorePullRequest', false)

    echo 'Cloning all Kubic Repos'

    timeout(5) {
        parallel 'automation': {
            cloneKubicRepo(branch: branch, ignorePullRequest: ignorePullRequest, repo: "automation")
        },
        'salt': {
            cloneKubicRepo(branch: branch, ignorePullRequest: ignorePullRequest, repo: "salt")
        },
        'velum': {
            cloneKubicRepo(branch: branch, ignorePullRequest: ignorePullRequest, repo: "velum")
        },
        'caasp-container-manifests': {
            cloneKubicRepo(branch: branch, ignorePullRequest: ignorePullRequest, repo: "caasp-container-manifests")
        }
    }
}
