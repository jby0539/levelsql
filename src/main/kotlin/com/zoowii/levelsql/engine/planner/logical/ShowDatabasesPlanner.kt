package com.zoowii.levelsql.engine.planner.logical

import com.zoowii.levelsql.engine.DbSession
import com.zoowii.levelsql.engine.executor.FetchTask
import com.zoowii.levelsql.engine.planner.LogicalPlanner
import com.zoowii.levelsql.engine.types.Chunk
import com.zoowii.levelsql.engine.types.Datum
import com.zoowii.levelsql.engine.types.DatumTypes
import com.zoowii.levelsql.engine.types.Row
import java.util.concurrent.Future


// 查询数据库列表的算子
class ShowDatabasesPlanner(private val sess: DbSession) : LogicalPlanner(sess) {
    private var executed = false
    override fun beforeChildrenTasksSubmit(fetchTask: FetchTask) {
        if(executed) {
            fetchTask.submitSourceEnd()
            return
        }
        val databases = sess.engine.listDatabases()
        val rows = mutableListOf<Row>()
        for(dbName in databases) {
            val row = Row()
            row.data = listOf(Datum(DatumTypes.kindString, stringValue = dbName))
            rows.add(row)
        }
        val chunk = Chunk().replaceRows(rows)
        executed = true
        fetchTask.submitChunk(chunk)
    }

    override fun afterChildrenTasksSubmitted(fetchTask: FetchTask, childrenFetchFutures: List<Future<FetchTask>>) {

    }

    override fun afterChildrenTasksDone(fetchTask: FetchTask, childrenFetchTasks: List<FetchTask>) {
        simplePassChildrenTasks(fetchTask, childrenFetchTasks)
    }

    override fun setSelfOutputNames() {
        setOutputNames(listOf("name"))
    }

}