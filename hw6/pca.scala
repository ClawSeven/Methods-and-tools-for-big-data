import org.apache.spark.mllib.feature.PCA
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
// import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LinearRegressionWithSGD

// Load and parse the data
val data = sc.textFile("pynum.csv").map { line => val parts = line.split(',')
  LabeledPoint(parts(parts.length-1).toDouble, 
  	Vectors.dense(parts.slice(0, parts.length).map(_.toDouble)))}.cache()

// Using PCA
val pca = new PCA(5).fit(data.map(_.features))
val projected = data.map(p => p.copy(features = pca.transform(p.features)))

// Building the model
val numIterations = 100
val stepSize = 0.00000001
val model = LinearRegressionWithSGD.train(projected, numIterations, stepSize)

// Evaluate model on training examples and compute training error
val valuesAndPreds = projected.map { point => val prediction = model.predict(point.features)
  (point.label, prediction)}
val MSE = valuesAndPreds.map{case(v, p) => math.pow((v - p), 2) }.mean()
println(s"training Mean Squared Error $MSE")



val Array(pta,ppd) = projected.randomSplit(Array(0.8, 0.2))
val model = LinearRegressionWithSGD.train(pta, numIterations, stepSize)
val valuesAndPreds = ppd.map { point => val prediction = model.predict(point.features)
  (point.label, prediction)}