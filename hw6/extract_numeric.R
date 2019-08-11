setwd("/Users/kelly/Desktop/2019_Summer/Ve572/hw/hw6")
house<-read.csv("house.csv",header=TRUE)

library(dplyr)
library(readr)

a = select_if(house, is.numeric)
a = na.omit(a)
save(a, file="house_num.RData")
write.csv(a,'numeric_house.csv') # automatically write header

# house2<-read.csv("numeric_house.csv",header=FALSE)
