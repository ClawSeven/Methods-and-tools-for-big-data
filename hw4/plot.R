setwd("/Users/kelly/Desktop/2019_Summer/Ve572/hw/hw4")
var <-read.csv("result/variance.csv",header=TRUE)
avg <-read.csv("result/average.csv",header=TRUE)

# load("RData/ycache.RData")
# save(data, file="RData/ycache.RData")

library(ggplot2)

ggplot() + 
  geom_point(data = avg, aes(x = cont, y = tavg), color="royalblue2", size = 2) +
  labs(y="average temperature (\u00B0C)",  # ASCII code for celcius
       x="continent",
       caption = "temperature variation of each continent")

ggplot(data = var, aes(x = cont, y = tvar)) + 
  geom_point(color="orange", size = 2) +
  labs(y=expression("temperature variance (\u00B0C)"^2),  # ASCII code for celcius
       x="continent",
       caption = "temperature variation of each continent")

