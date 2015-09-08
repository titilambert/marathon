package mesosphere.marathon.core.launcher

import org.rogach.scallop.ScallopConf

trait OfferProcessorConfig extends ScallopConf {
  //scalastyle:off magic.number

  lazy val offerMatchingTimeout = opt[Int]("offer_matching_timeout",
    descr = "Offer matching timeout (ms). Stop trying to match additional tasks for this offer after this time.",
    default = Some(1000))

  lazy val saveTasksToLaunchTimeout = opt[Int]("save_tasks_to_launch_timeout",
    descr = "Timeout (ms) for saving all tasks for one offer that we are about to launch.",
    default = Some(3000),
    hidden = true)

  lazy val declineOfferDuration = opt[Long]("decline_offer_duration",
    descr = "(Default: 120 seconds) " +
      "The duration (milliseconds) for which to decline offers by default",
    default = Some(120000))
}
