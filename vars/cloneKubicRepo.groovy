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
// Clones a single Kubic Repo.
def call(Map parameters = [:]) {
    def gitBase = parameters.get('gitBase')
    def branch = parameters.get('branch')
    def credentialsId = parameters.get('credentialsId')
    boolean ignorePullRequest = parameters.get('ignorePullRequest', false)
    def repo = parameters.get('repo')

    echo "Cloning Kubic Repo: ${repo}"

    timeout(5) {
        dir(repo) {
            if (!ignorePullRequest && env.JOB_NAME.contains(repo)) {
                if (env.CHANGE_ID) {
                    echo 'Attempting rebase...'

                    checkout([
                        $class: 'GitSCM',
                        branches:  [[name: "*/${env.CHANGE_TARGET}"]],
                        extensions: [
                            [$class: 'LocalBranch'],
                            [$class: 'CleanCheckout']
                        ],
                        userRemoteConfigs: [
                            [url:"${gitBase}/${repo}.git", credentialsId: credentialsId]
                        ]
                    ])

                    def gitVars = checkout scm
                    def rebaseScript = "git -c 'user.name=${gitVars.GIT_COMMITTER_NAME}' -c 'user.email=${gitVars.GIT_COMMITTER_EMAIL}' rebase ${env.CHANGE_TARGET}"
                    def rebaseCode = sh(script: rebaseScript, returnStatus: true)

                    if (rebaseCode) {
                        sh('git rebase --abort')
                        error("Rebase failed with code: '${rebaseCode}'. Manual rebase required.")
                    } else {
                        echo 'Rebase successful!'
                    }
                } else {
                    checkout scm
                }

            } else {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${branch}"]],
                    userRemoteConfigs: [
                        [url: "${gitBase}/${repo}.git", credentialsId: credentialsId]
                    ],
                    extensions: [[$class: 'CleanCheckout']],
                ])
            }
        }
    }
}
