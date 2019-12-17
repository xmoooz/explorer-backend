package org.ergoplatform.explorer.http.api.v1.defs

import org.ergoplatform.explorer.{Err, Id}
import org.ergoplatform.explorer.http.api.v1.models.BlockSummary
import sttp.tapir._

object BlocksEndpointDefs {

  private val PathPrefix = "blocks"

  def blockSummaryById: Endpoint[Id, Err, BlockSummary, Nothing] =
    baseEndpointDef
      .in(PathPrefix)
      .in(path[Id])
      .out(jsonBody[BlockSummary])
}