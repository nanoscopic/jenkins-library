import com.suse.kubic.BuildState

def call(name, Map parameters = [:], Closure block) {
    boolean ignoreFailure = parameters.get('ignoreFailure', false)
    boolean skip = parameters.get('skip', false)
    boolean skipOnFailure = parameters.get('skipOnFailure', true)

    if (skip) {
        stage(name) {
            echo "Skipping stage ${name} as requested"
        }
    } else if (skipOnFailure && BuildState.kubicStageFailed) {
        stage(name) {
            echo "Skipping stage ${name} as previous stage failed"
        }
    } else {
        try {
            stage(name, block)
        } catch (Exception exc) {
            if (ignoreFailure) {
                // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                echo "Ignoring stage ${name} failure"
            } else {
                BuildState.kubicStageFailed = true
            }
        }
    }
}
