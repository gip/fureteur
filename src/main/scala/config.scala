

package fureteur.config

import com.rabbitmq.client._

case class Config(channel:Channel, limit: Int)