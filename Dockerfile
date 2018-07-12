FROM yerlibilgin/jdk-8u172-stripped

WORKDIR /

ARG SCALA_HOME=/scala

ENV SCALA_HOME=$SCALA_HOME
ENV PATH=$SCALA_HOME/bin:$PATH

RUN wget https://downloads.lightbend.com/scala/2.11.12/scala-2.11.12.tgz && \
    tar -xvzf scala-2.11.12.tgz && \
    ln -s /scala-2.11.12 $SCALA_HOME && \
    chmod +x $SCALA_HOME/bin/scala && \
    chmod +x $SCALA_HOME/bin/scalac

RUN mkdir minder

WORKDIR /minder

COPY target/universal/stage/ startminder ./

EXPOSE 9000

CMD ["./startminder"]
