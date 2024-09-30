# Big Data 
1. Volume High. In the level of TB, PB.
2. Variety. large variety of unstructured data from multiple sources.    
    We need Data fusion
3. Velocity. Big data will have continuous stream of data at a very high rate.
大数据就是总量大，形式多，速率快。

分析大数据
1. Visulization 可视化
2. Querying Capabilities 
3. Predictive Analysis

# Big Data Processing

Example:
we have a stream of data coming into our system.
In real-time we want to have visualizaiton and balabala

Strategy:
1. Batch Processing. We process data in batch.
    It can run once per day, or once per month. 
    We don't need to process analysis in real-time. But one day per batch is already good enough.

    Pros:
    - Easy to implement. Latency no worries.
    - High avaliability.
    - Better efficiency. In batch is usually better.
    - Higher fault tolerance.
    - Complex and deep analysis
    Cons:
    - Long delay. No real-time analysis.
    Stocking exchange system we can not do this.

2. Real Time Processing. It is harder to do.


# Lambda Architecture

Batch processing vs Real Time Processing: 
Fast response time vs Deep Analysis, that is a tradeoff.

How to have both?

For example, the Log/Metrics Analysis Service.

Lambda Architecture:
It attempts to find balance between 
- high fault tolerance and comprehensive analysis of data.
- Low Latency

Layers of Lambda Architecture
Data goes to both Batch Layer and Speed Layer simutaneously.

- Batch Layer
    - Manage dataset and be a system of records. Distributed file systems.
    - Pre-compute batch views.
    - Generate a batch views.

- Speed Layer
    - Message Broker real-time processing jobs.
    - It works only on most recent data.
    - Generate a real-time views.

- Serving Layer
    - Query can query all batch and real-time views.

Ad-Tech Industry example:

Content Producers,