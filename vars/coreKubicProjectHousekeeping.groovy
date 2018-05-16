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
def call() {
    echo "Starting Kubic core project housekeeping"

    // TODO: Don't hardcode salt repo name, find the right place
    // to lookup this information dynamically.
    githubCollaboratorCheck(
        org: 'kubic-project',
        repo: 'salt',
        user: env.CHANGE_AUTHOR,
        credentialsId: 'github-token')

    def label = "housekeeping-${UUID.randomUUID().toString()}"

    podTemplate(label: label, containers: [
        containerTemplate(
            name: 'opensuse',
            image: 'opensuse:42.3',
            ttyEnabled: true,
            command: 'cat',
            envVars: [
                envVar(key: 'http_proxy', value: env.http_proxy),
                envVar(key: 'https_proxy', value: env.http_proxy),
            ],
        ),
    ]) {
        node(label) {
            stage('Retrieve Code') {
                checkout scm
            }

            stage('GitHub Labels') {
                // If this is a Pull Request build...
                if (env.CHANGE_ID) {
                    String changeTarget = env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME)
                    
                    echo "Add a backport label if needed"
                    if (changeTarget.matches(/release-\d\.\d/) && !pullRequest.labels.contains("${changeTarget}-backport")) {
                        echo "Adding backport label: ${changeTarget}-backport"
                        pullRequest.addLabels(["${changeTarget}-backport".toString()])
                    }

                    // Remove any invalid backport labels
                    // TODO: Disabled due to plugin issue re parsing GitHub JSON - add later once fixed.
                    // echo "Remove any invalid backport labels"
                    // def prLabels = pullRequest.labels
                    // prLabels.each { prLabel ->
                    //     echo "Checking label: ${prLabel}"
                    //     if (prLabel.matches(/release-\d\.\d-backport/) && prLabel != changeTarget + '-backport') {
                    //         echo "Removing label: ${prLabel}"
                    //         pullRequest.removeLabel(prLabel.toString())
                    //         echo "Removed label: ${prLabel}"
                    //     }
                    // }
                } else {
                    echo "Not a PR, no PR labels required"
                }
            }
        }
    }
}
