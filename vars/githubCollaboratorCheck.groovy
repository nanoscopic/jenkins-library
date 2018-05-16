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
def call(Map parameters = [:]) {
    echo "Starting GitHub Collaborator Check"

    String org = parameters.get('org')
    String repo = parameters.get('repo')
    String user = parameters.get('user')
    String credentialsId = parameters.get('credentialsId')

    if (env.CHANGE_AUTHOR != null) {
        // Check if a change is from collaborator, or not.
        // Require approval for non-collaborators. As non-collaborators are
        // already considered untrusted by Jenkins, Jenkins will load the
        // Pipeline and library from the target branch and NOT from the
        // outside collaborators fork / pull request.
        stage('Collaborator Check') {
            def membersResponse = httpRequest(
                url: "https://api.github.com/repos/${org}/${repo}/collaborators/${user}",
                authentication: credentialsId,
                validResponseCodes: "204:404")

            if (membersResponse.status == 204) {
                echo "Test execution for collaborator ${user} allowed"

            } else {
                def allowExecution = input(id: 'userInput', message: "Change author is not a ${org} member: ${user}", parameters: [
                    booleanParam(name: 'allowExecution', defaultValue: false, description: 'Run tests anyway?')
                ])

                if (!allowExecution) {
                    echo "Test execution for unknown user (${user}) disallowed"
                    error(message: "Test execution for unknown user (${user}) disallowed")
                    return;
                }
            }
        }
    }
}
