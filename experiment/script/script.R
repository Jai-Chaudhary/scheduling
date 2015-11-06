library(dplyr)
library(ggplot2)

readFile <- function(filename) {
  b <- basename(filename)
  b <- substr(b, 1, nchar(b) - 4)
  t <- strsplit(b, '_')[[1]]
  volunteerProbability <- as.numeric(t[1])
  patientConfidenceLevel <- as.numeric(t[2])
  advanceTime <- as.numeric(t[3])
  seed <- as.numeric(t[4])
  read.csv(filename, stringsAsFactor=F) %>%
    mutate(volunteerProbability=volunteerProbability,
           patientConfidenceLevel=patientConfidenceLevel,
           advanceTime=advanceTime,
           seed=seed) %>%
    mutate(wait=pmax(0, begin - pmax(arrival, appointment)))
}

readData <- function() {
  do.call(rbind,
          lapply(list.files('../data', full.names=T), readFile)
          )
}

data <- readData()

stat <- data %>% group_by(volunteerProbability, patientConfidenceLevel, advanceTime) %>% summarize(meanw = mean(wait),
                                                                                                   w30 = sum(wait>30),
                                                                                                   w45 = sum(wait>45),
                                                                                                   w60 = sum(wait>60),
                                                                                                   w75 = sum(wait>75),
                                                                                                   w90 = sum(wait>90),
                                                                                                   diversion=sum(site != originalSite),
                                                                                                   n=n())
write.csv(stat, 'stat.csv', quote=F, row.names=F)

divert <- data %>% filter(originalSite != site)
#pair <- data %>% filter(site != originalSite) %>% tbl_df %>% select(name, seed, volunteerProbability, patientConfidenceLevel, advanceTime, wait.new=wait) %>% left_join(
#  data %>% filter(volunteerProbability == 0, patientConfidenceLevel == 0, advanceTime == 60), c('name', 'seed')) %>%
#  select(name, seed, volunteerProbability=volunteerProbability.x, patientConfidenceLevel=patientConfidenceLevel.x, advanceTime=advanceTime.x, wait.new, wait.old=wait)
#write.csv(pair, 'pair.csv', quote=F, row.names=F)
#
#pair %>% group_by(volunteerProbability, patientConfidenceLevel, advanceTime) %>% summarize(diff = mean(wait.old - wait.new)) %>% data.frame()
