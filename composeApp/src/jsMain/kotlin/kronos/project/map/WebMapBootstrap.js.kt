@file:Suppress("unused")

package kronos.project.map


private var legacyHandle: WebMapHandle? = null

@Suppress("UNUSED_PARAMETER")
fun initializeWebMap(
    config: WebMapConfig,
    initialPins: List<IssuePin>,
    callbacks: WebMapCallbacks = WebMapCallbacks(),
) {
    legacyHandle?.destroy()
    legacyHandle = createBucharestMap(config.containerId)
}

@Suppress("UNUSED_PARAMETER")
fun setIssuePins(pins: List<IssuePin>) {
}

@Suppress("UNUSED_PARAMETER")
fun setSelectedIssuePin(pinId: String?) {
}
