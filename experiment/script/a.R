library(dplyr)
library(ggplot2)

readFile <- function(filename) {
  b <- basename(filename)
  b <- substr(b, 1, nchar(b) - 4)
  t <- strsplit(b, '_')[[1]]
  volunteerProbability <- as.numeric(t[1])
  seed <- as.numeric(t[2])
  read.csv(filename, stringsAsFactor=F) %>%
    mutate(volunteerProbability=volunteerProbability,
           seed=seed)
}

wait <- do.call(rbind, lapply(list.files('../data/wait', full.names=T), readFile))
overtime <- do.call(rbind, lapply(list.files('../data/overtime', full.names=T), readFile))

wait %>% group_by(volunteerProbability) %>% summarize(
                                                      w15 = sum(wait > 15) / n(),
                                                      w30 = sum(wait > 30) / n(),
                                                      w45 = sum(wait > 45) / n(),
                                                      w60 = sum(wait > 60) / n(),
                                                      w90 = sum(wait > 90) / n(),
                                                      w120 = sum(wait > 120) / n(),
                                                      meanw = mean(wait),
                                                      divert = sum(diverted == 'True') / n()) %>%
                                                        print

overtime %>% group_by(volunteerProbability) %>% summarize(meano = mean(overtime)) %>% print

(overtime %>% ggplot(aes(as.factor(volunteerProbability), overtime)) + geom_boxplot()) %>% print
