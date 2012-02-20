
package fureteur.time

import java.util.concurrent.TimeUnit

object Time {
  def sNow() = { msNow/1000 }
  def msNow() ={ (new java.util.Date()).getTime() }
}
